
(ns docs2.detect.engine
    (:require [docs2.detect.state :as detect.state]
              [io.api             :as io]
              [regex.api          :refer [re-match?]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn detect-code-files!
  ; @param (map) options
  ; {}
  ;
  ; @usage
  ; (detect-code-files! {...})
  ;
  ; @example
  ; (detect-code-files! {:code-dirs ["submodules/my-repository/source-code"]})
  ; =>
  ; {"submodules/my-repository/source-code/my_file.clj" {}}
  [{:keys [code-dirs filename-pattern] :as options}]
  (letfn [
          ; Returns whether the filename in the given filepath matches the filename pattern.
          (f0 [filepath] (let [filename (io/filepath->filename filepath)]
                              (re-match? filename filename-pattern)))

          ; Puts the given filepath into the detected files list in case of
          ; no filename pattern specified or the filename matches the pattern.
          (f1 [filepath] (if (or (not filename-pattern) (f0 filepath))
                             (swap! detect.state/DETECTED-CODE-FILES assoc filepath {})))

          ; Reads the file list of the given code-dir and iterates over the files
          ; to determine which files matches the filename pattern and to puts
          ; the matches into the detected files list
          (f2 [code-dir] (doseq [filepath (io/all-file-list code-dir)]
                                (f1 filepath)))]

         (reset! detect.state/DETECTED-CODE-FILES nil)
         (doseq [code-dir code-dirs]
                (f2 code-dir))))
