
(ns docs.detect.helpers
    (:require [candy.api         :refer [return]]
              [docs.detect.state :as detect.state]
              [string.api        :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn code-dir
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (string) alias
  [{:keys [abs-path code-dirs]} layer-name api-filepath _]
  (letfn [(f [[code-dir %]] (if (= % api-filepath)
                                (return code-dir)))]
         (let [api-files (get-in @detect.state/LAYERS [layer-name])]
              (some f api-files))))
