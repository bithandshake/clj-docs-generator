
(ns docs3.import.engine)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-def-symbol-endpoint
  ;
  ; (defn my-function [] ...)
  ; (def A my-function)
  ; (def B A)
  ; (def C B)
  ;
  [])

(defn def-symbol-endpoint-fn?
  ; függvényre mutat-e az endpoint?
  [])

(defn def-symbol?
  ; (def MY-CONSTANT my-symbol)
  [])

(defn def-fn?
  ;
  ; (def MY-FUNCTION (fn [] ...))
  ; Ezt is függvénynek olvassa be mint a defn-t
  [])

(defn import-namespaces
  ; egy file több namespace-t tartalmazhat
  [options filepath]
  (if-let [file-content (io/read-file filepath {:warn? true})]
          (letfn [(f [])])

          (println (str "Unable to read file: '" filepath "'"))))
