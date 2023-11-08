
(ns docs3.core.engine
    (:require [docs3.core.patterns   :as core.patterns]
              [docs3.core.prototypes :as core.prototypes]
              ;[docs3.detect.engine   :as detect.engine]
              ;[docs3.detect.state    :as detect.state]
              ;[docs3.import.engine   :as import.engine]
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
  ; The 'create-documentation!' function erases the output directory before exporting the documentation files!
  ;
  ; @param (map) options
  ; {:author (string)(opt)
  ;  :code-dirs (strings in vector)
  ;  :filename-pattern (regex pattern)(opt)
  ;   Default: #"[a-z\_\d]{1,}\.clj[cs]{0,1}"
  ;  :lib-name (string)
  ;  :output-dir (string)
  ;  :print-format (keyword)(opt)
  ;   :html, :md
  ;   Default: :md
  ;  :print-options (keywords in vector)(opt)
  ;   [:code, :credit, :description, :example, :param, :preview, :require, :return, :usage, :warning]
  ;   Default: [:code :credit :description :example :param :preview :require :return :usage :warning]
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
  (if (v/valid? options {:pattern* core.patterns/OPTIONS-PATTERN})
      (let [options (core.prototypes/options-prototype options)]
           (try (do (detect.engine/detect-code-files! options))
                    ;(import.engine/import-code-files! options))
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
