
(ns docs.detect.helpers
    (:require [docs.detect.state :as detect.state]
              [noop.api          :refer [return]]
              [string.api        :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn code-dir
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (string) alias
  ;
  ; @usage
  ; (code-dir {...} "clj" "submodules/my-repository/source/code/clj/my_directory/api.clj" "api")
  ;
  ; @example
  ; (code-dir {...} "clj" "submodules/my-repository/source/code/clj/my_directory/api.clj" "api")
  ; =>
  ; "source-code/clj"
  ;
  ; @return (string)
  [{:keys [abs-path code-dirs]} layer-name api-filepath _]
  ; Finds out the code-dir belongs to the taken api-filepath file.
  (letfn [(f [[code-dir %]] (if (= % api-filepath)
                                (return code-dir)))]
         (let [api-files (get-in @detect.state/LAYERS [layer-name])]
              (some f api-files))))
