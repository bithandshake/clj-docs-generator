
(ns docs.core.prototypes
    (:require [string.api :as string]
              [vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn options-prototype
  ; @param (map) options
  ;
  ; @return (map)
  ; {:print-options (keywords in vector)}
  [options]
  (letfn [(f [%] (-> % (string/not-starts-with! "/")
                       (string/not-ends-with!   "/")))]
         (merge {:print-options [:code :description :examples :params :require :return :usages :warning]}
                (-> options (update :abs-path   f)
                            (update :output-dir f)
                            (update :code-dirs  #(vector/->items % f))))))
