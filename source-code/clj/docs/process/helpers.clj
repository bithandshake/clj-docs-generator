
(ns docs.process.helpers
    (:require [docs.import.state :as import.state]
              [string.api        :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn clj-library?
  ; @return (boolean)
  []
  (or (-> (get @import.state/LAYERS "clj")  empty? not)
      (-> (get @import.state/LAYERS "cljc") empty? not)))

(defn cljs-library?
  ; @return (boolean)
  []
  (or (-> (get @import.state/LAYERS "cljc") empty? not)
      (-> (get @import.state/LAYERS "cljs") empty? not)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn md-path
  ; @param (map) options
  ;  {:abs-path (string)
  ;   :output-dir (string)}
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @usage
  ; (md-path {...} "clj" "submodules/my-repository/source-code/api.clj")
  ;
  ; @example
  ; (md-path {:abs-path "submodules/my-repository" :output-dir "/documentation"}
  ;          "clj" "submodules/my-repository/source-code/my_directory/api.clj")
  ; =>
  ; "submodules/my-repository/clj/my_directory"
  ;
  ; @return (string)
  [{:keys [abs-path output-dir]} layer-name api-filepath]
  ; It is important to separate the doc files by layers because it is possible
  ; to use the same namespace in both the "clj", "cljc" and "cljs" layers!
  (let [api-namespace (get-in @import.state/LAYERS [layer-name api-filepath "namespace"])]
       (str abs-path "/" output-dir "/" layer-name "/" (-> api-namespace (string/not-ends-with! ".api")
                                                                         (string/replace-part   "." "/")))))
