
;; -- Namespace ---------------------------------------------------------------
;; ----------------------------------------------------------------------------

(ns docs.core
    (:require [docs.config       :as config]
              [docs.prototypes   :as prototypes]
              [docs.state        :as state]
              [io.api            :as io]
              [mid-fruits.candy  :refer [return]]
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

(defn read-redirects!
  [api-content]
  (letfn [(f [result api-content cursor]
             (let [api-content (string/part api-content cursor)]
                  (if-let [cursor (string/first-index-of api-content "(def ")]
                          (let [symbol (-> api-content (string/after-first-occurence  "(def ")
                                                       (string/before-first-occurence " "))]
                               (f (assoc result symbol true) api-content (inc cursor)))
                          (return result))))]
         (f {} api-content 0)))

(defn read-api!
  [api-filepath]
  (read-redirects! (io/read-file api-filepath)))

(defn read-directory!
  ; @param (map) options
  ;  {:path (string)}
  ; @param (string) layer
  ;  "clj", "cljc", "cljs"
  ; @param (string) directory-name
  ;
  ; @return (?)
  [{:keys [path] :as options} layer directory-name]
  (let [api-filepath (str path "/source-code/"layer"/"directory-name"/api."layer)]
       (if (io/file-exists? api-filepath)
           (read-api!       api-filepath))))

(defn read-layer!
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
                       (assoc result directory-name (read-directory! options layer directory-name))))]
              (reduce f {} directory-list))))

(defn read-layers!
  ; @param (map) options
  ;  {:path (string)}
  [{:keys [path] :as options}]
  (letfn [(f [result layer]
             (let [layer-path (str path "/source-code/" (name layer))]
                  (if (io/directory-exists? layer-path)
                      (assoc result layer (read-layer! options layer)))))]
         (reset! state/LAYERS (reduce f {} ["clj" "cljc" "cljs"]))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn create-documentation!
  ; @param (map) options
  ;  {:path (string)}
  ;
  ; @usage
  ;  (docs/create-documentation! {...})
  ;
  ; @usage
  ;  (docs/create-documentation! {:path "submodules/my-repository/"})
  [options]
  (let [options (prototypes/options-prototype options)]
       (initialize!  options)
       (read-layers! options)
       (str @state/LAYERS)))
