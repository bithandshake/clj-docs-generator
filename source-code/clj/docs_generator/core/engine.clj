
(ns docs-generator.core.engine
    (:require [docs-generator.core.prototypes :as core.prototypes]
              [docs-generator.core.tests      :as core.tests]
              [docs-generator.detect.engine   :as detect.engine]
              [docs-generator.detect.state    :as detect.state]
              [docs-generator.import.engine   :as import.engine]
              [docs-generator.import.state    :as import.state]
              [docs-generator.print.engine    :as print.engine]
              [docs-generator.process.engine  :as process.engine]
              [docs-generator.process.state   :as process.state]
              [docs-generator.read.engine     :as read.engine]
              [docs-generator.read.state      :as read.state]
              [fruits.regex.api               :refer [re-match?]]
              [fruits.string.api              :as string]
              [io.api                         :as io]
              [validator.api                  :as v]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn initialize!
  ; @ignore
  ;
  ; @param (map) options
  [options]
  (reset! detect.state/LAYERS  nil)
  (reset! import.state/LAYERS  nil)
  (reset! process.state/COVER  nil)
  (reset! process.state/COMMON nil)
  (reset! process.state/LAYERS nil)
  (reset! read.state/LAYERS    nil)
  (if (-> options :output-dir io/directory-exists?)
      (-> options :output-dir io/empty-directory!)
      (-> options :output-dir io/create-directory!)))

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
       "\n\ndetected layers:\n"  (get-in @detect.state/LAYERS  [])
       "\n\nimported layers:\n"  (get-in @import.state/LAYERS  [])
       "\n\nread layers:\n"      (get-in @read.state/LAYERS    [])
       "\n\nprocessed layers:\n" (get-in @process.state/LAYERS [])
       "\n\nprocessed cover:\n"  (get-in @process.state/COVER  [])
       "\n\nprocessed common:\n" (get-in @process.state/COMMON [])
       "</pre>"))

(defn create-documentation!
  ; @important
  ; The 'create-documentation!' function erases the output directory before printing the documentation books!
  ;
  ; @param (map) options
  ; {:author (string)(opt)
  ;  :code-dirs (strings in vector)
  ;  :filename-pattern (regex pattern)(opt)
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
  (if (v/valid? options core.tests/OPTIONS-TEST {:prefix "options"})
      (let [options (core.prototypes/options-prototype options)]
           (initialize!                    options)
           (detect.engine/detect-layers!   options)
           (import.engine/import-layers!   options)
           (read.engine/read-layers!       options)
           (process.engine/process-layers! options)
           (process.engine/process-cover!  options)
           (process.engine/process-common! options)
           (print.engine/print-cover!      options)
           (print.engine/print-layers!     options)
           (debug))))
