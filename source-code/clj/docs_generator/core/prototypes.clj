
(ns docs-generator.core.prototypes
    (:require [fruits.string.api :as string]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn options-prototype
  ; @ignore
  ;
  ; @param (map) options
  ;
  ; @return (map)
  ; {:print-options (keywords in vector)}
  [options]
  (letfn [(f0 [%] (-> % (string/not-starts-with! "/")
                        (string/not-ends-with!   "/")))]
         (merge {:print-options [:code :credits :description :examples :params :require :return :usages :warning]}
                (-> options (update :output-dir f0)
                            (update :code-dirs  #(vector/->items % f0))))))
