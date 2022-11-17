
(ns docs.import.engine
    (:require [candy.api           :refer [return]]
              [docs.import.helpers :as import.helpers]
              [docs.import.state   :as import.state]
              [io.api              :as io]
              [string.api          :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-first-refer
  ; @param (string) api-content
  ; @param (integer) cursor
  ;
  ; @example
  ; (import-first-refer "... [my-namespace :refer [my-refer]] [your-namespace :refer [your-refer]] ..." 42)
  ; =>
  ; {"my-refer" "my-namespace"}
  ;
  ; @return (map)
  [api-content cursor]
  (-> api-content (string/part cursor)
                  (import.helpers/first-refer)))

(defn import-refers
  ; @param (string) api-content
  ;
  ; @example
  ; (import-refers "... [my-namespace :refer [my-refer]] [your-namespace :refer [your-refer]] ...")
  ; =>
  ; {"my-refer"   "my-namespace"
  ;  "your-refer" "your-namespace"}
  ;
  ; @return (map)
  [api-content]
  (letfn [(f [refers n]
             (if-let [cursor (string/nth-dex-of api-content "[" n)]
                     (let [refer (import-first-refer api-content cursor)]
                          (f (merge refers refer)
                             (inc n)))
                     (return refers)))]
         (f {} 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-first-alias
  ; @param (string) api-content
  ; @param (integer) cursor
  ;
  ; @example
  ; (import-first-alias "... [my-namespace :as my-alias] [your-namespace :as your-alias] ..." 42)
  ; =>
  ; {"my-namespace" "my-alias"}
  ;
  ; @return (map)
  [api-content cursor]
  (let [[namespace alias] (-> api-content (string/part cursor)
                                          (import.helpers/first-alias))]
       {namespace alias}))

(defn import-aliases
  ; @param (string) api-content
  ;
  ; @example
  ; (import-aliases "... [my-namespace :as my-alias] [your-namespace :as your-alias] ...")
  ; =>
  ; {"my-namespace"   "my-alias"
  ;  "your-namespace" "your-alias"}
  ;
  ; @return (map)
  [api-content]
  (letfn [(f [aliases n]
             (if-let [cursor (string/nth-dex-of api-content "[" n)]
                     (let [alias (import-first-alias api-content cursor)]
                          (f (merge aliases alias)
                             (inc n)))
                     (return aliases)))]
         (f {} 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-first-def
  ; @param (string) api-content
  ; @param (integer) cursor
  ;
  ; @example
  ; (import-first-def "... (def my-name my-value) (def your-name your-value) ..." 42)
  ; =>
  ; ["my-name" "my-value"]
  ;
  ; @return (strings in vector)
  [api-content cursor]
  (-> api-content (string/part cursor)
                  (import.helpers/first-def)))

(defn import-defs
  ; @param (string) api-content
  ;
  ; @example
  ; (import-defs "... (def my-name my-value) (def your-name your-value) ...")
  ; =>
  ; {"my-name"   "my-value"
  ;  "your-name" "your-value"}
  ;
  ; @return (strings in vectors in vector)
  [api-content]
  (letfn [(f [defs n]
             (if-let [cursor (string/nth-dex-of api-content "(def " n)]
                     (let [def (import-first-def api-content cursor)]
                          (f (conj defs def)
                             (inc n)))
                     (return defs)))]
         (f [] 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-directory
  ; @param (map) options
  ; {:path (string)}
  ; @param (string) layer-name
  ; "clj", "cljc", "cljs"
  ; @param (string) directory-name
  ;
  ; @example
  ; (import-directory "my-submodules/my-repository/source-code/clj/my_directory/api.clj")
  ; =>
  ; {"aliases" {"my-namespace"   "my-alias"
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
  [{:keys [path] :as options} layer-name directory-name]
  (let [api-filepath (import.helpers/api-filepath options layer-name directory-name)
        api-content  (io/read-file api-filepath)]
       {"aliases" (import-aliases api-content)
        "defs"    (import-defs    api-content)
        "refers"  (import-refers  api-content)}))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-layer
  ; @param (map) options
  ; {:path (string)}
  ; @param (string) layer
  ; "clj", "cljc", "cljs"
  ;
  ; @example
  ; (import-layer {:path "my-submodules/my-repository"} "clj")
  ; =>
  ; {"my_directory" {"aliases" {"my-namespace"   "my-alias"
  ;                             "your-namespace" "your-alias"}
  ;                  "defs"    [["my-name"   "my-value"]
  ;                             ["your-name" "your-value"]]
  ;                  "refers"  {"my-refer"   "my-namespace"
  ;                             "your-refer" "your-namespace"}}}
  ;
  ; @return (map)
  [{:keys [path] :as options} layer-name]
  (let [layer-path     (import.helpers/layer-path options layer-name)
        directory-list (io/subdirectory-list layer-path)]
       (letfn [(f [layer-data directory-path]
                  (let [directory-name (io/directory-path->directory-name directory-path)
                        api-filepath   (import.helpers/api-filepath options layer-name directory-name)]
                       (if (io/file-exists? api-filepath)
                           (assoc  layer-data directory-name (import-directory options layer-name directory-name))
                           (return layer-data))))]
              (reduce f {} directory-list))))

(defn import-layers
  ; @param (map) options
  ; {:path (string)}
  ;
  ; @example
  ; (import-layers {:path "my-submodules/my-repository"})
  ; =>
  ; {"clj" {"my_directory" {"aliases" {"my-namespace"   "my-alias"
  ;                                    "your-namespace" "your-alias"}
  ;                         "defs"    [["my-name"   "my-value"]
  ;                                    ["your-name" "your-value"]]
  ;                         "refers"  {"my-refer"   "my-namespace"
  ;                                    "your-refer" "your-namespace"}}}}
  ;  "cljc" {...}
  ;  "cljs" {...}}
  ;
  ; @return (map)
  ; {"clj" (map)
  ;  "cljc" (map)
  ;  "cljs" (map)}
  [{:keys [path] :as options}]
  (letfn [(f [result layer-name]
             (let [layer-path (import.helpers/layer-path options layer-name)]
                  (if (io/directory-exists? layer-path)
                      (assoc  result layer-name (import-layer options layer-name))
                      (return result))))]
         (reduce f {} ["clj" "cljc" "cljs"])))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-layers!
  ; @param (map) options
  [options]
  (let [layers (import-layers options)]
       (reset! import.state/LAYERS layers)))
