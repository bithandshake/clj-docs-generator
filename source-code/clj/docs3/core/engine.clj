
(ns docs3.core.engine
    (:require [docs3.core.tests :as core.tests]
              [docs3.core.prototypes :as core.prototypes]
              [source-code-map.api   :as source-code-map]
              [validator.api         :as v]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; (defn my-function [] ...)
; (def A my-function)
; (def B A)
; (def C B)
;
; legyen benne a state-ben az is hogy honnan hova min keresztül van átirányitva
; és ezt fel kék tüntetni a doksiban is a teljes redirection trace-t:
;
; This function is redirected [ajax.api/send-request! > ajax.side-effects/send-request!]
; This constant is redirected [my-library.api/MY-CONSTANT > my-library.config/MY-CONSTANT > iso.my-library.config/MY-CONSTANT]

; függvényre mutat-e az endpoint?
; (defn def-symbol-fn? [])

; (def MY-CONSTANT my-symbol)
; (defn def-symbol? [])

; (def MY-FUNCTION (fn [] ...))
; Ezt is függvénynek olvassa be mint a defn-t
; (defn def-fn? [])

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
  ;  :source-paths (strings in vector)
  ;  :filename-pattern (regex pattern)(opt)
  ;   Default: #"[a-z\_\d]{1,}\.clj[cs]{0,1}"
  ;  :lib-name (string)
  ;  :output-path (string)
  ;  :previews-path (string)(opt)
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
  (if (v/valid? options core.tests/OPTIONS-TEST)
      (let [options (core.prototypes/options-prototype options)])))
           ;(println (source-code-map/read-source-files {:source-paths ["dependencies/ajax-api/source-code"]
                                                          ;:filename-pattern #"[a-z\-\_\d]\.clj[cs]{1,1}"})))))
            ;                                              :filename-pattern #"test.clj")])))
           ;(try (do (detect.engine/detect-code-files! options))
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
;                (catch Exception e (println e))])))
