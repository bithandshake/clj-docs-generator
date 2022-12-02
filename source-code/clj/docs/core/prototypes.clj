
(ns docs.core.prototypes
    (:require [string.api :as string]
              [vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn options-prototype
  ; @param (map) options
  ;
  ; @return
  [options]
  (letfn [(f [%] (-> % (string/not-starts-with! "/")
                       (string/not-ends-with!   "/")))]
         (-> options (update :abs-path   f)
                     (update :output-dir f)
                     (update :code-dirs  #(vector/->items % f)))))
