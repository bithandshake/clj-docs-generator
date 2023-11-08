
(ns docs3.core.utils
    (:require [string.api :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn valid-directory-path
  ; @ignore
  ;
  ; @param (string) directory-path
  ;
  ; @return (string)
  [directory-path]
  (-> directory-path (string/not-starts-with! "/")
                     (string/not-ends-with!   "/")))
