
(ns docs2.import.engine
    (:require [docs2.detect.state :as detect.state]
              [docs2.import.config :as import.config]
              [docs2.import.helpers :as import.helpers]
              [docs2.import.state :as import.state]
              [io.api            :as io]
              [regex.api         :as regex :refer [re-match?]]
              [string.api        :as string]))

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
  ; 1. Detects constants in the given file content
  ; 2. Iterates over the detected constant matches
  ; 3. Derives the constant names from each match
  ; 4. Stores the constant names and their occurence positions in file into the imported files atom
  (doseq [match (re-seq import.config/CONSTANT-PATTERN file-content)]
         (if-not (import.helpers/constant-ignored? file-content match)
                 (if-let [constant-name (import.helpers/match->constant-name match)]
                         (swap! import.state/IMPORTED-FILES assoc-in [filepath :constants constant-name]
                                                                     (string/first-dex-of file-content match))))))

(defn import-functions!
  ; @param (map) options
  ; @param (string) filepath
  ; @param (string) file-content
  ;
  ; @usage
  ; (import-functions! {...} "..." "...")
  [options filepath file-content]
  (doseq [match (re-seq import.config/FUNCTION-PATTERN file-content)]))
                 ;(println match))))

(defn import-redirects!
  ; @param (map) options
  ; @param (string) filepath
  ; @param (string) file-content
  ;
  ; @usage
  ; (import-redirects! {...} "..." "...")
  [options filepath file-content]
  (doseq [match (re-seq import.config/REDIRECT-PATTERN file-content)]
         (if-not (import.helpers/constant-ignored? file-content match)
                 (if-let [constant-name (import.helpers/match->constant-name match)]
                         (swap! import.state/IMPORTED-FILES assoc-in [filepath :redirects constant-name]
                                                                     (string/first-dex-of file-content match))))))

(defn import-files!
  ; @param (map) options
  ;
  ; @usage
  ; (import-files! {...})
  [options]
  (doseq [[filepath _] @detect.state/DETECTED-FILES]
         (when-let [file-content (io/read-file filepath)]
                   (import-constants! options filepath file-content)
                   (import-functions! options filepath file-content)
                   (import-redirects! options filepath file-content)))
  (println @import.state/IMPORTED-FILES))
