
(ns docs.reader
    (:require [candy.api         :refer [return]]
              [docs.helpers      :as helpers]
              [docs.state        :as state]
              [io.api            :as io]
              [mid-fruits.string :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-first-refer
  ; @param (string) api-content
  ; @param (integer) cursor
  ;
  ; @example
  ;  (read-first-refer "... [my-namespace :refer [my-refer]] [your-namespace :refer [your-refer]] ..." 42)
  ;  =>
  ;  {"my-refer" "my-namespace"}
  ;
  ; @return (map)
  [api-content cursor]
  (-> api-content (string/part cursor)
                  (helpers/first-refer)))

(defn read-refers
  ; @param (string) api-content
  ;
  ; @example
  ;  (read-refers "... [my-namespace :refer [my-refer]] [your-namespace :refer [your-refer]] ...")
  ;  =>
  ;  {"my-refer"   "my-namespace"
  ;   "your-refer" "your-namespace"}
  ;
  ; @return (map)
  [api-content]
  (letfn [(f [refers n]
             (if-let [cursor (string/nth-dex-of api-content "[" n)]
                     (let [refer (read-first-refer api-content cursor)]
                          (f (merge refers refer)
                             (inc n)))
                     (return refers)))]
         (f {} 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-first-alias
  ; @param (string) api-content
  ; @param (integer) cursor
  ;
  ; @example
  ;  (read-first-alias "... [my-namespace :as my-alias] [your-namespace :as your-alias] ..." 42)
  ;  =>
  ;  {"my-namespace" "my-alias"}
  ;
  ; @return (map)
  [api-content cursor]
  (let [[namespace alias] (-> api-content (string/part cursor)
                                          (helpers/first-alias))]
       {namespace alias}))

(defn read-aliases
  ; @param (string) api-content
  ;
  ; @example
  ;  (read-aliases "... [my-namespace :as my-alias] [your-namespace :as your-alias] ...")
  ;  =>
  ;  {"my-namespace"   "my-alias"
  ;   "your-namespace" "your-alias"}
  ;
  ; @return (map)
  [api-content]
  (letfn [(f [aliases n]
             (if-let [cursor (string/nth-dex-of api-content "[" n)]
                     (let [alias (read-first-alias api-content cursor)]
                          (f (merge aliases alias)
                             (inc n)))
                     (return aliases)))]
         (f {} 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-first-def
  ; @param (string) api-content
  ; @param (integer) cursor
  ;
  ; @example
  ;  (read-first-def "... (def my-name my-value) (def your-name your-value) ..." 42)
  ;  =>
  ;  {"my-name" "my-value"}
  ;
  ; @return (map)
  [api-content cursor]
  (let [[name value] (-> api-content (string/part cursor)
                                     (helpers/first-def))]
       {name value}))

(defn read-defs
  ; @param (string) api-content
  ;
  ; @example
  ;  (read-defs "... (def my-name my-value) (def your-name your-value) ...")
  ;  =>
  ;  {"my-name"   "my-value"
  ;   "your-name" "your-value"}
  ;
  ; @return (map)
  [api-content]
  (letfn [(f [defs n]
             (if-let [cursor (string/nth-dex-of api-content "(def " n)]
                     (let [def (read-first-def api-content cursor)]
                          (f (merge defs def)
                             (inc n)))
                     (return defs)))]
         (f {} 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-directory
  ; @param (map) options
  ;  {:path (string)}
  ; @param (string) layer-name
  ;  "clj", "cljc", "cljs"
  ; @param (string) directory-name
  ;
  ; @example
  ;  (read-directory "my-submodules/my-repository/source-code/clj/my-directory/api.clj")
  ;  =>
  ;  {"aliases" {"my-namespace"   "my-alias"
  ;              "your-namespace" "your-alias"}
  ;   "defs"    {"my-name"        "my-value"
  ;              "your-name"      "your-value"}
  ;   "refers"  {"my-refer"       "my-namespace"
  ;              "your-refer"     "your-namespace"}}
  ;
  ; @return (map)
  ;  {"aliases" (map)
  ;   "defs" (map)
  ;   "refers" (map)}
  [{:keys [path] :as options} layer-name directory-name]
  (let [api-filepath (helpers/api-filepath options layer-name directory-name)
        api-content  (io/read-file api-filepath)]
       {"aliases" (read-aliases api-content)
        "defs"    (read-defs    api-content)
        "refers"  (read-refers  api-content)}))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-layer
  ; @param (map) options
  ;  {:path (string)}
  ; @param (string) layer
  ;  "clj", "cljc", "cljs"
  ;
  ; @example
  ;  (read-layer {:path "my-submodules/my-repository"} "clj")
  ;  =>
  ;  {"my-directory" {"aliases" {"my-namespace"   "my-alias"
  ;                              "your-namespace" "your-alias"}
  ;                   "defs"    {"my-name"        "my-value"
  ;                              "your-name"      "your-value"}
  ;                   "refers"  {"my-refer"       "my-namespace"
  ;                              "your-refer"     "your-namespace"}}}
  ;
  ; @return (map)
  [{:keys [path] :as options} layer-name]
  (let [layer-path     (helpers/layer-path options layer-name)
        directory-list (io/subdirectory-list layer-path)]
       (letfn [(f [layer-data directory-path]
                  (let [directory-name (io/directory-path->directory-name directory-path)
                        api-filepath   (helpers/api-filepath options layer-name directory-name)]
                       (if (io/file-exists? api-filepath)
                           (assoc  layer-data directory-name (read-directory options layer-name directory-name))
                           (return layer-data))))]
              (reduce f {} directory-list))))

(defn read-layers
  ; @param (map) options
  ;  {:path (string)}
  ;
  ; @example
  ;  (read-layers {:path "my-submodules/my-repository"})
  ;  =>
  ;  {"clj" {"my-directory" {"aliases" {"my-namespace"   "my-alias"
  ;                                     "your-namespace" "your-alias"}
  ;                          "defs"    {"my-name"        "my-value"
  ;                                     "your-name"      "your-value"}
  ;                          "refers"  {"my-refer"       "my-namespace"
  ;                                     "your-refer"     "your-namespace"}}}}
  ;   "cljc" {...}
  ;   "cljs" {...}}
  ;
  ; @return (map)
  ;  {"clj" (map)
  ;   "cljc" (map)
  ;   "cljs" (map)}
  [{:keys [path] :as options}]
  (letfn [(f [result layer-name]
             (let [layer-path (helpers/layer-path options layer-name)]
                  (if (io/directory-exists? layer-path)
                      (assoc result layer-name (read-layer options layer-name)))))]
         (reduce f {} ["clj" "cljc" "cljs"])))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-layers!
  ; @param (map) options
  [options]
  (let [layers (read-layers options)]
       (reset! state/LAYERS layers)))
