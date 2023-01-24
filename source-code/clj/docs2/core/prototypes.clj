
(ns docs2.core.prototypes
    (:require [string.api :as string]
              [vector.api :as vector]))

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
  (letfn [(f [%] (-> % (string/not-starts-with! "/")
                       (string/not-ends-with!   "/")))]
         (merge {:filename-pattern #"[a-z\_\d]{1,}\.clj[cs]{0,}"
                 :print-options [:code :credits :description :examples :params :require :return :usages :warning]}
                (-> options (update :output-dir f)
                            (update :code-dirs  #(vector/->items % f))))))
