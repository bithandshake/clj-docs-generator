
(ns docs.core.helpers)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn output-path
  ; @param (map) options
  ;
  ; @return (string)
  [{:keys [abs-path output-dir]}]
  (str abs-path "/" output-dir))
