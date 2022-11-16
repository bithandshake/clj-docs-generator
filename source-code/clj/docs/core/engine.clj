
(ns docs.core.engine
    (:require [candy.api            :refer [return]]
              [docs.core.prototypes :as core.prototypes]
              [docs.detect.engine   :as detect.engine]
              [docs.detect.state    :as detect.state]
              [docs.import.engine   :as import.engine]
              [docs.import.state    :as import.state]
              [docs.print.engine    :as print.engine]
              [docs.process.engine  :as process.engine]
              [docs.process.state   :as process.state]
              [docs.read.engine     :as read.engine]
              [docs.read.state      :as read.state]
              [io.api               :as io]
              [regex.api            :refer [re-match?]]
              [mid-fruits.string    :as string]))



;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn initialize!
  ; @param (map) options
  ;  {:path (string)}
  [{:keys [path] :as options}]
  (reset! detect.state/API-FILES nil)
  (reset! import.state/LAYERS    nil)
  (reset! process.state/COVER    nil)
  (reset! process.state/LAYERS   nil)
  (reset! read.state/LAYERS      nil)
  (let [directory-path (str path "/documentation")]
       (if (io/directory-exists? directory-path)
           (io/empty-directory!  directory-path)
           (io/create-directory! directory-path))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn debug
  []
  (str "<pre style=\"background:#fafafa\">"
       "\nimported layers:\n"
       (get-in @import.state/LAYERS  [])
       "\nread layers:\n"
       (get-in @read.state/LAYERS    [])
       "\nprocessed layers:\n"
       (get-in @process.state/LAYERS [])
       "</pre>"))

(defn create-documentation!
  ; @param (map) options
  ;  {:path (string)}
  ;
  ; @usage
  ;  (create-documentation! {...})
  ;
  ; @usage
  ;  (create-documentation! {:path "my-submodules/my-repository"})
  ;
  ; @return (string)
  [options]
  (let [options (core.prototypes/options-prototype options)]
       (initialize!                    options)
       (detect.engine/detect-layers!   options)
       (import.engine/import-layers!   options)
       (read.engine/read-layers!       options)
       (process.engine/process-layers! options)
       (process.engine/process-cover!  options)
       (print.engine/print-cover!      options)
       (print.engine/print-layers!     options)
       (debug)))
