
(ns docs2.core.engine
    (:require [docs2.core.patterns   :as core.patterns]
              [docs2.core.prototypes :as core.prototypes]
              [docs2.detect.engine   :as detect.engine]
              [docs2.detect.state    :as detect.state]
              [docs2.import.engine   :as import.engine]

              ;[docs.import.engine   :as import.engine]
              ;[docs.import.state    :as import.state]
              ;[docs.print.engine    :as print.engine]
              ;[docs.process.engine  :as process.engine]
              ;[docs.process.state   :as process.state]
              ;[docs.read.engine     :as read.engine]
              ;[docs.read.state      :as read.state]
              [pattern.api          :as p]))

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
  (if (p/valid? options {:pattern* core.patterns/OPTIONS-PATTERN})
      (let [options (core.prototypes/options-prototype options)]
           (try ;(initialize!                    options))
                (do (detect.engine/detect-files! options)
                    (import.engine/import-files! options))
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
