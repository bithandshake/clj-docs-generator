
(ns docs-generator.detect.state)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @atom (map)
;  {"clj" (map)
;   "cljc" (map)
;   "cljs" (map)}
(def LAYERS (atom {}))

; @ignore
;
; @atom (map)
(def CODE-FILES (atom {}))
