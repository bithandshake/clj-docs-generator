
(ns docs-generator.import.engine
    (:require [docs-generator.detect.state :as detect.state]
              [docs-generator.import.state :as import.state]
              [docs-generator.import.utils :as import.utils]
              [fruits.string.api           :as string]
              [io.api                      :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-first-refer
  ; @ignore
  ;
  ; @param (string) api-content
  ; @param (integer) cursor
  ;
  ; @usage
  ; (import-first-refer "... [my-namespace :refer [my-refer]] [your-namespace :refer [your-refer]] ..." 42)
  ; =>
  ; {"my-refer" "my-namespace"}
  ;
  ; @return (map)
  [api-content cursor]
  (-> api-content (string/keep-range cursor)
                  (import.utils/first-refer)))

(defn import-refers
  ; @ignore
  ;
  ; @param (string) api-content
  ;
  ; @usage
  ; (import-refers "... [my-namespace :refer [my-refer]] [your-namespace :refer [your-refer]] ...")
  ; =>
  ; {"my-refer"   "my-namespace"
  ;  "your-refer" "your-namespace"}
  ;
  ; @return (map)
  [api-content]
  (letfn [(f0 [refers skip]
              (if-let [cursor (string/nth-dex-of api-content "[" skip)]
                      (let [refer (import-first-refer api-content cursor)]
                           (f0 (merge refers refer)
                              (inc   skip)))
                      (-> refers)))]
         (f0 {} 0)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-namespace
  ; @ignore
  ;
  ; @param (string) api-content
  ;
  ; @return (string)
  [api-content]
  (-> api-content (string/after-first-occurence  "(ns " {:return? false})
                  (string/before-first-occurence " "    {:return? false})
                  (string/not-ends-with!         "\n")))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-first-alias
  ; @ignore
  ;
  ; @param (string) api-content
  ; @param (integer) cursor
  ;
  ; @usage
  ; (import-first-alias "... [my-namespace :as my-alias] [your-namespace :as your-alias] ..." 42)
  ; =>
  ; {"my-namespace" "my-alias"}
  ;
  ; @return (map)
  [api-content cursor]
  (let [[namespace alias] (-> api-content (string/keep-range cursor)
                                          (import.utils/first-alias))]
       {namespace alias}))

(defn import-aliases
  ; @ignore
  ;
  ; @param (string) api-content
  ;
  ; @usage
  ; (import-aliases "... [my-namespace :as my-alias] [your-namespace :as your-alias] ...")
  ; =>
  ; {"my-namespace"   "my-alias"
  ;  "your-namespace" "your-alias"}
  ;
  ; @return (map)
  [api-content]
  (letfn [(f0 [aliases skip]
              (if-let [cursor (string/nth-dex-of api-content "[" skip)]
                      (let [alias (import-first-alias api-content cursor)]
                           (f0 (merge aliases alias)
                              (inc  skip)))
                     (-> aliases)))]
         (f0 {} 0)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-first-def
  ; @ignore
  ;
  ; @param (string) api-content
  ; @param (integer) cursor
  ;
  ; @usage
  ; (import-first-def "... (def my-name my-value) (def your-name your-value) ..." 42)
  ; =>
  ; ["my-name" "my-value"]
  ;
  ; @return (strings in vector)
  [api-content cursor]
  (-> api-content (string/keep-range cursor)
                  (import.utils/first-def)))

(defn import-defs
  ; @ignore
  ;
  ; @param (string) api-content
  ;
  ; @usage
  ; (import-defs "... (def my-name my-value) (def your-name your-value) ...")
  ; =>
  ; {"my-name"   "my-value"
  ;  "your-name" "your-value"}
  ;
  ; @return (strings in vectors in vector)
  [api-content]
  (letfn [(f0 [defs skip]
              (if-let [cursor (string/nth-dex-of api-content "(def " skip)]
                      (let [def (import-first-def api-content cursor)]
                           (f0 (conj defs def)
                              (inc  skip)))
                      (-> defs)))]
         (f0 [] 0)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-api-file
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) layer-name
  ; "clj", "cljc", "cljs"
  ; @param (string) api-filepath
  ;
  ; @usage
  ; (import-api-file {...} "clj" "my-repository/source-code/my_directory/api.clj")
  ; =>
  ; {"namespace" "my-directory.api"
  ;  "aliases" {"my-namespace"   "my-alias"
  ;             "your-namespace" "your-alias"}
  ;  "defs"    [["my-name"   "my-value"]
  ;             ["your-name" "your-value"]]
  ;  "refers"  {"my-refer"   "my-namespace"
  ;             "your-refer" "your-namespace"}}
  ;
  ; @return (map)
  ; {"aliases" (map)
  ;  "defs" (strings in vectors in vector)
  ;  "refers" (map)}
  [_ layer-name api-filepath]
  (let [api-content (io/read-file     api-filepath)]
       {"namespace" (import-namespace api-content)
        "aliases"   (import-aliases   api-content)
        "defs"      (import-defs      api-content)
        "refers"    (import-refers    api-content)}))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-layer
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) layer
  ; "clj", "cljc", "cljs"
  ;
  ; @usage
  ; (import-layer {...} "clj")
  ; =>
  ; {"my-repository/source-code/my_directory/api.clj" {"namespace" "api"
  ;                                                    "aliases" {"my-namespace"   "my-alias"
  ;                                                               "your-namespace" "your-alias"}
  ;                                                    "defs"    [["my-name"   "my-value"]
  ;                                                               ["your-name" "your-value"]]
  ;                                                    "refers"  {"my-refer"   "my-namespace"
  ;                                                               "your-refer" "your-namespace"}}}
  ;
  ; @return (map)
  [options layer-name]
  (let [api-files (get @detect.state/LAYERS layer-name)]
       (letfn [(f0 [result [_ api-filepath]]
                   (assoc result api-filepath (import-api-file options layer-name api-filepath)))]
              (reduce f0 {} api-files))))

(defn import-layers
  ; @ignore
  ;
  ; @param (map) options
  ;
  ; @usage
  ; (import-layers {...})
  ; =>
  ; {"clj" {"my-repository/source-code/my_directory/api.clj" {"namespace" "api"
  ;                                                           "aliases" {"my-namespace"   "my-alias"}
  ;                                                                      "your-namespace" "your-alias"}
  ;                                                           "defs"    [["my-name"   "my-value"]
  ;                                                                      ["your-name" "your-value"]]
  ;                                                           "refers"  {"my-refer"   "my-namespace"
  ;                                                                      "your-refer" "your-namespace"}}}}
  ;  "cljc" {...}
  ;  "cljs" {...}}
  ;
  ; @return (map)
  ; {"clj" (map)
  ;  "cljc" (map)
  ;  "cljs" (map)}
  [options]
  (letfn [(f0 [result layer-name api-files]
              (assoc result layer-name (import-layer options layer-name)))]
         (reduce-kv f0 {} @detect.state/LAYERS)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-layers!
  ; @ignore
  ;
  ; @param (map) options
  [options]
  (let [layers (import-layers options)]
       (reset! import.state/LAYERS layers)))
