(ns vending.test.core
  (:use clojure.test
        ring.mock.request
        vending.core))

(defn member?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))


(deftest test-add-coin
  (testing
    "Test adding coins to machine"
    (let [machine (make-default-machine)
          merk-machine (add-coin machine :merk)
          plack-machine (add-coin machine :plack)
          bawbee-machine (add-coin machine :bawbee)
          bodle-machine (add-coin machine :bodle)
          all-machine (add-coin (add-coin (add-coin (add-coin machine :merk) :plack) :bawbee) :bodle) ]
      (is (member? (:tendered merk-machine) :merk) "after adding a merk, a merk should be present")
      (is (member? (:tendered all-machine) :merk) "after adding a merk, a merk should be present")
      (is (member? (:tendered plack-machine) :plack) "after adding a plack, a plack should be present")
      (is (member? (:tendered all-machine) :plack) "after adding a plack, a plack should be present")
      (is (member? (:tendered bawbee-machine) :bawbee) "after adding a bawbee, a bawbee should be present")
      (is (member? (:tendered all-machine) :bawbee) "after adding a bawbee, a bawbee should be present")
      (is (member? (:tendered bodle-machine) :bodle) "after adding a bodle, a bodle should be present")
      (is (member? (:tendered all-machine) :bodle) "after adding a merk, a merk should be present")
      (is (nil? (:tendered machine)) "No coins should leak through into the base machine")))

  (testing
    "coin-return should return all coins added"
    (let [machine (make-default-machine)
          all-machine (add-coin (add-coin (add-coin (add-coin machine :merk) :plack) :bawbee) :bodle)
          return-machine (coin-return all-machine)]
      (is (= (:message return-machine)) "Coins returned")
      (is (= (:tendered all-machine) (:change return-machine)) "All coins should be returned")
    ))

  (testing
    "summing coins"
    (let [coins1 (sum-coins (list :merk :plack :bawbee :bodle))
          coins2 (sum-coins (list :merk :plack :bawbee :bodle :merk :plack :bawbee :bodle))]
      (= (:merk coins1 1) "after adding one merk, one merk should be present")
      (= (:merk coins2 2) "after adding two merks, two merks should be present")
      ))

  (testing
    "making appropriate change"
    (let [no-change (make-change 0 5 5 5 5)
          ten-change (make-change 10 5 5 5 5)
          all-change (make-change 140 5 5 5 5)]
      (is (= no-change '(0 0 0 0)) "one hundred units of change should be satisfied with one merk")
      (is (= all-change '(1 1 1 1)) "140 units of change should be satisfied with one coin of each denomination")
      (is (or (= ten-change '(0 0 1 0)) (= ten-change '(0 0 0 2))) "ten change could be satisfied with one bawbee or two bodles")))

  (testing
    "removing change from machine"
    (let [machine (make-default-machine)
          changed (make-change-machine machine '(1 1 1 1))
          emptied (make-change-machine machine '(1 4 4 4))]
      (is (= (vals (:coins changed)) '(0 3 3 3)) "each coin slot should be decremented by one")
      (is (= (vals (:coins emptied)) '(0 0 0 0)) "each coin slot should be empty (contain zero)")
      ))
  )

  (make-change-machine (make-default-machine) '( 1 1 1 1))

