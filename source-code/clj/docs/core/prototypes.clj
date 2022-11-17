
(ns docs.core.prototypes
    (:require [candy.api  :refer [param]]
              [string.api :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn options-prototype
  ; @param (map) options
  ; {}
  ;
  ; @return
  ; {}
  [{:keys [path] :as options}]
  (merge {}
         (param options)
         {:path (-> path (string/not-starts-with! "/")
                         (string/not-ends-with!   "/"))}))
