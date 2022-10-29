
(ns docs.core
    (:require [docs.config       :as config]
              [docs.helpers      :as helpers]
              [docs.prototypes   :as prototypes]
              [docs.state        :as state]
              [io.api            :as io]
              [mid-fruits.candy  :refer [return]]
              [mid-fruits.regex  :refer [re-match?]]
              [mid-fruits.string :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn initialize!
  ; @param (map) options
  ;  {:path (string)}
  [{:keys [path] :as options}]
  (let [directory-path (str path "/documentation")]
       (if (io/directory-exists? directory-path)
           (io/empty-directory!  directory-path)
           (io/create-directory! directory-path))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-refer
  [api-content cursor])
  ; TODO

(defn read-refers
  [api-content])
  ; TODO

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-alias
  ; @param (string) api-content
  ; @param (integer) cursor
  ;
  ; @return (map)
  [api-content cursor]
  (let [[namespace alias] (-> api-content (string/part cursor)
                                          (helpers/first-alias))]
       {namespace alias}))

(defn read-aliases
  ; @param (string) api-content
  ;
  ; @return (map)
  [api-content]
  (letfn [(f [result n]
             (if-let [cursor (string/nth-dex-of api-content "[" n)]
                     (let [alias (read-alias api-content cursor)]
                          (f (merge result alias)
                             (inc n)))
                     (return result)))]
         (f {} 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-def
  ; @param (string) api-content
  ; @param (integer) cursor
  ;
  ; @return (map)
  [api-content cursor]
  (let [[symbol value] (-> api-content (string/part cursor)
                                       (helpers/first-def))]
       {symbol value}))

(defn read-defs
  ; @param (string) api-content
  ;
  ; @return (map)
  [api-content]
  (letfn [(f [result n]
             (if-let [cursor (string/nth-dex-of api-content "(def " n)]
                     (let [def (read-def api-content cursor)]
                          (f (merge result def)
                             (inc n)))
                     (return result)))]
         (f {} 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-api
  ; @param (string) api-filepath
  ;
  ; @return (map)
  ;  {"aliases" (map)
  ;   "defs" (map)
  ;   "refers" (map)}
  [api-filepath]
  (let [api-content (io/read-file api-filepath)]
       {"aliases" (read-aliases api-content)
        "defs"    (read-defs    api-content)
        "refers"  (read-refers  api-content)}))

(defn read-directory
  ; @param (map) options
  ;  {:path (string)}
  ; @param (string) layer
  ;  "clj", "cljc", "cljs"
  ; @param (string) directory-name
  ;
  ; @return (map)
  [{:keys [path] :as options} layer directory-name]
  (let [api-filepath (str path "/source-code/"layer"/"directory-name"/api."layer)]
       (if (io/file-exists? api-filepath)
           (read-api        api-filepath))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-layer
  ; @param (map) options
  ;  {:path (string)}
  ; @param (string) layer
  ;  "clj", "cljc", "cljs"
  ;
  ; @return (map)
  [{:keys [path] :as options} layer]
  (let [layer-path     (str path "/source-code/"layer)
        directory-list (io/subdirectory-list layer-path)]
       (letfn [(f [result directory-path]
                  (let [directory-name (io/directory-path->directory-name directory-path)]
                       (assoc result directory-name (read-directory options layer directory-name))))]
              (reduce f {} directory-list))))

(defn read-layers
  ; @param (map) options
  ;  {:path (string)}
  ;
  ; @return (map)
  ;  {"clj" (map)
  ;   "cljc" (map)
  ;   "cljs" (map)}
  [{:keys [path] :as options}]
  (letfn [(f [result layer]
             (let [layer-path (str path "/source-code/" (name layer))]
                  (if (io/directory-exists? layer-path)
                      (assoc result layer (read-layer options layer)))))]
         (reduce f {} ["clj" "cljc" "cljs"])))

(defn import-layers!
  ; @param (map) options
  [options]
  (let [layers (read-layers options)]
       (reset! state/LAYERS layers)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

 ;; ----------------------------------------------------------------------------
 ;; ----------------------------------------------------------------------------

(defn process-def!
  ; @param (map) options
  ; @param (string) layer
  ; @param (string) directory-name
  ; @param (string) symbol
  ; @param (string) value
  [options layer directory-name symbol value])

(defn process-directory!
  ; @param (map) options
  ; @param (string) layer
  ; @param (string) directory-name
  ; @param (map) directory-data
  [options layer directory-name directory-data]
  (letfn [(f [_ symbol value]
             (process-def! options layer directory-name symbol value))]
         (reduce-kv f {} directory-data)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-layer!
  ; @param (map) options
  ; @param (string) layer
  ; @param (map) layer-data
  [options layer layer-data]
  (letfn [(f [_ directory-name directory-data]
             (process-directory! options layer directory-name directory-data))]
         (reduce-kv f {} layer-data)))

(defn process-layers!
  ; @param (map) options
  [options]
  (let [layers @state/LAYERS]
       (letfn [(f [_ layer layer-data]
                  (process-layer! options layer layer-data))]
              (reduce-kv f {} layers))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn create-documentation!
  ; @param (map) options
  ;  {:path (string)}
  ;
  ; @usage
  ;  (create-documentation! {...})
  ;
  ; @usage
  ;  (create-documentation! {:path "submodules/my-repository/"})
  [options]
  (let [options (prototypes/options-prototype options)]
       (initialize!     options)
       (import-layers!  options)
       (process-layers! options)
       (str @state/LAYERS)))
