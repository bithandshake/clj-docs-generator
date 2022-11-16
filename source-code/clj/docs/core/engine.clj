
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
       ;(get-in @import.state/LAYERS  ["clj"])
       ;"------------------------------------------------"
       ;(get-in @read.state/LAYERS ["clj"])
       (get-in (some (fn [%] (if (= "copy-file!" (get % "name")) %))
                     (get-in @read.state/LAYERS ["clj" "io" "functions"]))
               ["header" "usages" 0 "call"])
       "</pre>"))
       ;"------------------------------------------------"
       ;(get-in @process.state/LAYERS  [])
       ;"------------------------------------------------")))
       ;(get-in @process.state/LAYERS ["clj"]))))

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
  ; @return (boolean)
  ;  Returns true if the documentation book generation went success otherwise returns false.
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
      ;(debug)
      (return true)))
