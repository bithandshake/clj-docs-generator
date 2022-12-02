
(ns docs.detect.engine
    (:require [candy.api         :refer [return]]
              [docs.detect.state :as detect.state]
              [io.api            :as io]
              [string.api        :as string]
              [vector.api        :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn detect-code-dir!
  ; @param (map) options
  ; @param (string) code-dir
  [{:keys [abs-path]} code-dir]
  (let [file-list (io/all-file-list (str abs-path "/" code-dir))]
       (letfn [(f [result filepath]
                  (cond (string/ends-with? filepath "api.clj")
                        (update result "clj"  vector/conj-item [code-dir filepath])
                        (string/ends-with? filepath "api.cljc")
                        (update result "cljc" vector/conj-item [code-dir filepath])
                        (string/ends-with? filepath "api.cljs")
                        (update result "cljs" vector/conj-item [code-dir filepath])
                        :return result))]
              (reduce f {} file-list))))

(defn detect-layers!
  ; @param (map) options
  ;
  ; @usage
  ; (detect-layers! {...})
  ;
  ; @example
  ; (detect-layers! {:abs-path "submodules/my-repository"
  ;                  :code-dirs ["source-code/clj"]})
  ; =>
  ; {"clj" [["source-code/clj" "submodules/my-repository/source-code/my_directory/api.clj"]]}
  ;
  ; @result (map)
  ; {"clj" (vectors in vector)
  ;  "cljc" (vectors in vector)
  ;  "cljs" (vectors in vector)}
  [{:keys [code-dirs] :as options}]
  (letfn [(f [layers code-dir]
             (let [{:strs [clj cljc cljs]} (detect-code-dir! options code-dir)]
                  (-> layers (update "clj"  vector/concat-items clj)
                             (update "cljc" vector/concat-items cljc)
                             (update "cljs" vector/concat-items cljs))))]
         (reset! detect.state/LAYERS (reduce f {} code-dirs))))
