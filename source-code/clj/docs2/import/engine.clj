
(ns docs2.import.engine
    (:require [docs2.detect.state :as detect.state]
              [docs2.import.config :as import.config]
              [docs2.import.utils :as import.utils]
              [docs2.import.state :as import.state]
              [io.api            :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-constants!
  ; @param (map) options
  ; @param (string) filepath
  ; @param (string) file-content
  ;
  ; @usage
  ; (import-constants! {...} "..." "...")
  [options filepath file-content]
  (doseq [match (re-seq import.config/CONSTANT-DECLARATION-PATTERN file-content)]
         (if-let [constant-name (import.utils/match->constant-name match)]
                 (if-not (import.utils/constant-ignored? file-content constant-name)
                         (let [constant-header (import.utils/constant-header file-content constant-name)
                               constant-value  (import.utils/constant-value  file-content constant-name)]
                              (swap! import.state/IMPORTED-CODE-FILES assoc-in [filepath :constants constant-name]
                                                                               {:header constant-header
                                                                                :value  constant-value}))))))

  ; Megvizsgálni, hogy a konstans átirányítás-e!
  ; Egy átirányítás mutathat ...
  ; ... egy további átirányításra
  ; ... egy másik konstansra
  ; ... egy függvényre

(defn import-functions!
  ; @param (map) options
  ; @param (string) filepath
  ; @param (string) file-content
  ;
  ; @usage
  ; (import-functions! {...} "..." "...")
  [options filepath file-content]
  (doseq [match (re-seq import.config/FUNCTION-DECLARATION-PATTERN file-content)]
         (if-let [function-name (import.utils/match->function-name match)]
                 (if-not (import.utils/function-ignored? file-content function-name)
                         (let [function-header (import.utils/function-header file-content function-name)
                               function-body   (import.utils/function-body   file-content function-name)]
                              (swap! import.state/IMPORTED-CODE-FILES assoc-in [filepath :functions function-name]
                                                                               {:header function-header
                                                                                :body   function-body}))))))

(defn import-code-files!
  ; @param (map) options
  ;
  ; @usage
  ; (import-code-files! {...})
  [options]
  (doseq [[filepath _] @detect.state/DETECTED-CODE-FILES]
         (when-let [file-content (io/read-file filepath)]
                   (import-constants! options filepath file-content)
                   (import-functions! options filepath file-content)))
  (println @import.state/IMPORTED-CODE-FILES))
