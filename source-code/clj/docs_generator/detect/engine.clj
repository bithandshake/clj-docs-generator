
(ns docs-generator.detect.engine
    (:require [docs-generator.detect.state :as detect.state]
              [fruits.string.api           :as string]
              [fruits.vector.api           :as vector]
              [io.api                      :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn detect-code-dir!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) code-dir
  [_ code-dir]
  (let [file-list (io/all-file-list code-dir {:warn? false})]
       (letfn [(f0 [result filepath]
                   (cond (string/ends-with? filepath "api.clj")
                         (update result "clj"  vector/conj-item [code-dir filepath])
                         (string/ends-with? filepath "api.cljc")
                         (update result "cljc" vector/conj-item [code-dir filepath])
                         (string/ends-with? filepath "api.cljs")
                         (update result "cljs" vector/conj-item [code-dir filepath])
                         :return result))]
              (reduce f0 {} file-list))))

(defn detect-layers!
  ; @ignore
  ;
  ; @param (map) options
  ;
  ; @usage
  ; (detect-layers! {:code-dirs ["submodules/my-repository/source-code"]})
  ; =>
  ; {"clj" [["source-code/clj" "submodules/my-repository/source-code/my_directory/api.clj"]]}
  ;
  ; @result (map)
  ; {"clj" (vectors in vector)
  ;  "cljc" (vectors in vector)
  ;  "cljs" (vectors in vector)}
  [{:keys [code-dirs] :as options}]
  (letfn [(f0 [layers code-dir]
              (let [{:strs [clj cljc cljs]} (detect-code-dir! options code-dir)]
                   (-> layers (update "clj"  vector/concat-items clj)
                              (update "cljc" vector/concat-items cljc)
                              (update "cljs" vector/concat-items cljs))))]
         (reset! detect.state/LAYERS (reduce f0 {} code-dirs))))
