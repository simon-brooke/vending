(ns vending.core)

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))



(defn make-default-machine [] { :stock {:apples 5 :oranges 5 :lemons 5}
	:coins {:dollars 1 :quarters 4 :dimes 4 :nickels 4}
	:tendered nil
	:last-message ""
	:change nil
	:output nil} )

(def coin-values {:dollar 1 :quarter 0.25 :dime 0.10 :nickel 0.05})

(def item-prices {:apples 0.65 :oranges 1 :lemons 1.5})

(defn coin-value [coin]
	(coin-values coin))

(defn coins-value [coins]
	(apply + (map coin-value coins)))

(defn coin-return [machine]
	(assoc (dissoc machine :tendered) :change (:tendered machine)))
 
(defn add-coin [machine coin]
	(assoc (dissoc machine :tendered) :tendered (cons coin (:tendered machine))))

(defn add-coins [machine coins]
	(cond (empty? coins) machine
		true (add-coins (add-coin machine (first coins)) (rest coins))))

(defn magic-inc [maybenum]
	(cond (nil? maybenum) 1
		true (inc maybenum)))

(defn sum-coin [coin sums]
	(cond
		(= coin :dollar) (update-in sums [:dollar] magic-inc) 
		(= coin :quarter) (update-in sums [:quarter] magic-inc) 
		(= coin :dime) (update-in sums [:dime] magic-inc) 
		(= coin :nickel) (update-in sums [:nickel] magic-inc)))

(defn sum-coins 
	"Returns a map {:dollars :quarters :dimes :nickels}"
	([coins]
		(sum-coins coins {}))
	([coins sums]
		(cond (empty? coins) sums
			true (sum-coins (rest coins) (sum-coin (first coins) sums)))))



(defn subtract-denomination [list position]
	(cond (= (count list) position)(cons (- (first list) 1) (rest list))
		true (cons (first list) (subtract-denomination (rest list) position))))

(defn subtract-nickle [list]
	(subtract-denomination list 1))

(defn subtract-dime [list]
	(subtract-denomination list 2))

(defn subtract-quarter [list]
	(subtract-denomination list 3))

(defn subtract-dollar [list]
	(subtract-denomination list 4))

(defn in-make-change [amount dollars quarters dimes nickels]
	"Return a tuple (dollars quarters nickels dimes) which indicates the number remaining 
	after making change, or nil if not possible"
	(cond
		(= amount 0) (list dollars quarters dimes nickels)
		(and (>= amount (:dollar coin-values)) (> dollars 0))
			(in-make-change (- amount (:dollar coin-values)) (- dollars 1) quarters dimes nickels)
		(and (>= amount (:quarter coin-values)) (> quarters 0))
			(in-make-change (- amount (:quarter coin-values)) dollars (- quarters 1) dimes nickels)
		(and (>= amount (:dime coin-values)) (> dimes 0))
			(in-make-change (- amount (:dime coin-values)) dollars quarters (- dimes 1) nickels)
		(and (>= amount (:nickel coin-values)) (> nickels 0))
			(in-make-change (- amount (:nickel coin-values)) dollars quarters dimes (- nickels 1))))

(defn make-change [amount dollars quarters dimes nickels]
	(map #(- %1 %2) (list dollars quarters dimes nickels) 
		(in-make-change amount dollars quarters dimes nickels)
		))

(defn make-change-machine [machine change]
	machine)

(defn remove-from-stock [machine item]
	machine)

(defn deliver-item [machine item change]
	(make-change-machine 
		(remove-from-stock 
			(assoc (dissoc machine :output) :output (cons item (:output machine))) 
			item) 
		change)) 


(defn get-item [machine item]
	(let [item-price (item item-prices)
		tend (sum-coins (:tendered machine))
		change (make-change item-price (:dollar tend) (:quarter tend) (:dime tend) (:nickel tend))]
	(cond (<= 0 (item (:stock machine))) (coin-return machine)
		(<= (coins-value (:tendered machine)) item-price) (coin-return machine)
		(empty? change) (coin-return)
		true (deliver-item machine item change))))

(defn get-apple [machine]
	(get-item machine :apples))


