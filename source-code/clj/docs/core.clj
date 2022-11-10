
(ns docs.core
    (:require [docs.config       :as config]
              [docs.engine       :as engine]
              [docs.helpers      :as helpers]
              [docs.prototypes   :as prototypes]
              [docs.reader       :as reader]
              [docs.state        :as state]
              [io.api            :as io]
              [mid-fruits.candy  :refer [return]]
              [regex.api         :refer [re-match?]]
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

(defn create-documentation!
  ; @param (map) options
  ;  {:path (string)}
  ;
  ; @usage
  ;  (create-documentation! {...})
  ;
  ; @usage
  ;  (create-documentation! {:path "my-submodules/my-repository"})
  [options]
  (let [options (prototypes/options-prototype options)]
       (initialize!            options)
       (reader/import-layers!  options)
       (engine/process-layers! options)
       (str (get-in @state/LAYERS ["clj"]))))
