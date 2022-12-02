
(ns docs.core.engine
    (:require [candy.api            :refer [return]]
              [docs.core.helpers    :as core.helpers]
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
              [string.api           :as string]))



;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn initialize!
  ; @param (map) options
  [options]
  (reset! detect.state/LAYERS  nil)
  (reset! import.state/LAYERS  nil)
  (reset! process.state/COVER  nil)
  (reset! process.state/LAYERS nil)
  (reset! read.state/LAYERS    nil)
  (if (-> options core.helpers/output-path io/directory-exists?)
      (-> options core.helpers/output-path io/empty-directory!)
      (-> options core.helpers/output-path io/create-directory!)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn debug
  [options]
  (str "<pre style=\"background:#fafafa\">"
       "\noptions:\n" options
       "\n\ndetected layers:\n"
       (get-in @detect.state/LAYERS  [])
       "\n\nimported layers:\n"
       (get-in @import.state/LAYERS  [])
       "\n\nread layers:\n"
       (get-in @read.state/LAYERS    [])
       "\n\nprocessed layers:\n"
       (get-in @process.state/LAYERS [])
       "</pre>"))

(defn create-documentation!
  ; @param (map) options
  ; {:abs-path (string)
  ;  :code-dirs (strings in vector)
  ;  :lib-name (string)
  ;  :output-dir (string)}
  ;
  ; @usage
  ; (create-documentation! {...})
  ;
  ; @usage
  ; (create-documentation! {:abs-path   "submodules/my-repository"
  ;                         :code-dirs  ["source-code/clj"]
  ;                         :output-dir "documentation"
  ;                         :lib-name   "my-repository"})
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
       (debug options)))
