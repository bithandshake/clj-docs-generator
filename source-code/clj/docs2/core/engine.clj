
(ns docs2.core.engine
    (:require [docs2.core.prototypes :as core.prototypes]
              [docs2.core.tests      :as core.tests]
              [docs2.detect.engine   :as detect.engine]
              [docs2.detect.state    :as detect.state]
              [docs2.import.engine   :as import.engine]
              [validator.api         :as v]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn debug
  ; @ignore
  ;
  ; @usage
  ; (debug)
  ;
  ; @return (string)
  []
  (str "<pre style=\"background:#fafafa\">"
       ;"\n\ndetected layers:\n"  (get-in @detect.state/LAYERS  [])
       ;"\n\nimported layers:\n"  (get-in @import.state/LAYERS  [])
       ;"\n\nread layers:\n"      (get-in @read.state/LAYERS    [])
       ;"\n\nprocessed layers:\n" (get-in @process.state/LAYERS [])
       ;"\n\nprocessed cover:\n"  (get-in @process.state/COVER  [])
       ;"\n\nprocessed common:\n" (get-in @process.state/COMMON [])
       "</pre>"))

(defn create-documentation!
  ; @warning
  ; The create-documentation! function erases the output-dir before printing
  ; the documentation books!
  ; Be careful with configuring this function!
  ;
  ; @param (map) options
  ; {:author (string)(opt)
  ;  :code-dirs (strings in vector)
  ;  :filename-pattern (regex pattern)(opt)
  ;   Default: #"[a-z\_\d]{1,}\.clj[cs]{0,}"
  ;  :lib-name (string)
  ;  :output-dir (string)
  ;  :print-options (keywords in vector)(opt)
  ;   [:code, :credits, :description, :examples, :params, :require, :return, :usages, :warning]
  ;   Default: [:code :credits :description :examples :params :require :return :usages :warning]
  ;  :website (string)(opt)}
  ;
  ; @usage
  ; (create-documentation! {...})
  ;
  ; @usage
  ; (create-documentation! {:author           "Author"
  ;                         :code-dirs        ["submodules/my-repository/source-code"]
  ;                         :filename-pattern "[a-z\-]\.clj"
  ;                         :output-dir       "submodules/my-repository/documentation"
  ;                         :lib-name         "My library"
  ;                         :website          "https://github.com/author/my-repository"})
  ;
  ; @return (string)
  [options]
  (if (v/valid? options core.tests/OPTIONS-TEST)
      (let [options (core.prototypes/options-prototype options)]
           (try ;(initialize!                    options))
                (do (detect.engine/detect-code-files! options)
                    (import.engine/import-code-files! options))
                ;(detect.engine/detect-layers!   options)
                ;(import.engine/import-layers!   options)
                ;(read.engine/read-layers!       options)
                ;(process.engine/process-layers! options)
                ;(process.engine/process-cover!  options)
                ;(process.engine/process-common! options)
                ;(print.engine/print-cover!      options)
                ;(print.engine/print-layers!     options)
                ;(debug))
                (catch Exception e (println e))))))
