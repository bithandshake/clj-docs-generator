
(ns docs.core.prototypes
    (:require [candy.api  :refer [param]]
              [string.api :as string]
              [vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn options-prototype
  ; @param (map) options
  ; {}
  ;
  ; @return
  ; {}
  [{:keys [abs-path code-dirs output-dir] :as options}]
  (letfn [(f [%] (-> % (string/not-starts-with! "/")
                       (string/not-ends-with!   "/")))]
         (-> options (update :abs-path   f)
                     (update :output-dir f)
                     (update :code-dirs  #(vector/->items % f)))))
