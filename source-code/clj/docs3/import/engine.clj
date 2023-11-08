
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
  ; legyen benne az atomban az is hogy honnan hova min keresztül van átirányitva
  ; és ezt fel kék tüntetni a doksiban is a teljes redirection trace-t:
  ;
  ; This function is redirected [ajax.api/send-request! > ajax.side-effects/send-request!]
  ; This constant is redirected [my-library.api/MY-CONSTANT > my-library.config/MY-CONSTANT > iso.my-library.config/MY-CONSTANT]
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
