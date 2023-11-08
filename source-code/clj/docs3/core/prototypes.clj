
(ns docs3.core.prototypes
    (:require [docs3.core.utils :as core.utils]
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
  ;  :filename-pattern (regex pattern)
  ;  :print-format (keyword)
  ;  :print-options (keywords in vector)
  ;  :output-dir (string)}
  [options]
  (merge {:filename-pattern #"[a-z\_\d]{1,}\.clj[cs]{0,1}"
          :print-format     :md
          :print-options    [:code :credit :description :example :param :preview :require :return :usage :warning]}
         (-> options (update :output-dir #(core.utils/valid-directory-path %))
                     (update :code-dirs  #(vector/->items % core.utils/valid-directory-path)))))
