
(ns docs-generator.process.state)

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
(def COVER (atom {}))

; @ignore
;
; @atom (map)
(def COMMON (atom {}))
