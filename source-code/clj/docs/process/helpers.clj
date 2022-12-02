
(ns docs.process.helpers
    (:require [docs.import.state :as import.state]
              [string.api        :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn clj-library?
  ; @return (boolean)
  []
  (or (get @import.state/LAYERS "clj")
      (get @import.state/LAYERS "cljc")))

(defn cljs-library?
  ; @return (boolean)
  []
  (or (get @import.state/LAYERS "cljs")
      (get @import.state/LAYERS "cljc")))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn md-path
  ; @param (map) options
  ;  {:abs-path (string)
  ;   :output-dir (string)}
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @return (string)
  [{:keys [abs-path output-dir]} layer-name api-filepath]
  (let [api-namespace (get-in @import.state/LAYERS [layer-name api-filepath "namespace"])]
       (str abs-path "/" output-dir "/" layer-name "/" (-> api-namespace (string/not-ends-with! ".api")
                                                                         (string/replace-part   "." "/")))))
