
(ns docs-generator.process.utils
    (:require [docs-generator.import.state :as import.state]
              [fruits.string.api           :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn md-path
  ; @ignore
  ;
  ; @param (map) options
  ; {:output-dir (string)}
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @usage
  ; (md-path {:output-dir "submodules/my-repository/documentation"}
  ;          "clj" "submodules/my-repository/source-code/my_directory/api.clj")
  ; =>
  ; "submodules/my-repository/clj/my_directory"
  ;
  ; @return (string)
  [{:keys [output-dir]} layer-name api-filepath]
  ; It is important to separate the doc files by layers because it is possible
  ; to use the same namespace in both the "clj", "cljc" and "cljs" layers!
  (let [api-namespace (get-in @import.state/LAYERS [layer-name api-filepath "namespace"])]
       (str output-dir "/" layer-name "/" (-> api-namespace (string/not-end-with ".api")
                                                            (string/replace-part "." "/")))))
