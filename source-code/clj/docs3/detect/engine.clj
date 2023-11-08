
(ns docs3.detect.engine
    (:require [docs3.detect.state :as detect.state]
              [io.api             :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn detect-code-files!
  ; @ignore
  ;
  ; @param (map) options
  ; {:code-dirs (strings in vector)
  ;  :filename-pattern (regex pattern)}
  ;
  ; @usage
  ; (detect-code-files! {...})
  ;
  ; @example
  ; (detect-code-files! {:code-dirs ["submodules/my-repository/source-code"]
  ;                      :filename-pattern #"..."})
  ; =>
  ; {"submodules/my-repository/source-code/my_file.clj" {}}
  [{:keys [code-dirs filename-pattern] :as options}]
  (letfn [; Searches for files in the source code directories which name's matches
          ; with the given filename pattern and stores the found filepaths in the 'DETECTED-FILES' atom.
          (f2 [code-dir] (doseq [filepath (io/search-files code-dir filename-pattern)]
                                (swap! detect.state/DETECTED-FILES assoc filepath {})))]

         ; ...
         (reset! detect.state/DETECTED-FILES nil)
         (doseq [code-dir code-dirs]
                (f2 code-dir))))
