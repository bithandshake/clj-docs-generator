
(ns docs2.core.prototypes
    (:require [docs2.core.utils :as core.utils]
              [vector.api       :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn options-prototype
  ; @ignore
  ;
  ; @param (map) options
  ;
  ; @return (map)
  ; {:code-dirs (strings in vector)
  ;  :filename-pattern (string)
  ;  :print-options (keywords in vector)
  ;  :output-dir (string)}
  [options]
  (merge {:filename-pattern #"[a-z\_\d]{1,}\.clj[cs]{0,}"
          :print-options [:code :credits :description :examples :params :require :return :usages :warning]}
         (-> options (update :output-dir #(core.utils/valid-directory-path %))
                     (update :code-dirs  #(vector/->items % core.utils/valid-directory-path)))))
