(ns vending.test.core
  (:use clojure.test
        ring.mock.request
        vending.core))

(deftest test-vending
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
      (is (= (:merk coins1) 1) "after adding one merk, one merk should be present")
      (is (= (:merk coins2) 2) "after adding two merks, two merks should be present")
      ))

  (testing
    "making appropriate change"
    (let [wallet {:merk 1 :plack 1 :bawbee 1 :bodle 1}
          no-change (make-change 0 wallet)
          merk-change (make-change (coin-values :merk) wallet)
          ten-change (make-change (+ (coin-values :plack) (coin-values :bawbee)) wallet)
          all-change (make-change (reduce + (map #(coin-values %) (keys coin-values))) wallet)]
      (is (empty? no-change) "no change implies an empty coin list")
      (is (= merk-change '(:merk)) "140 units of change should be satisfied with one mark")
      (is (= (set all-change) (set (list :merk :plack :bawbee :bodle))) "172 units of change should be satisfied with one coin of each denomination")
      (is (= (set ten-change) (set (list :plack :bawbee))) "ten change could be satisfied with one bawbee or two bodles")))

  (testing
    "removing change from machine"
    (let [machine (make-default-machine)
          changed (make-change-machine machine '(:merk :plack :bawbee :bodle))
          emptied (make-change-machine machine '(:merk :plack :bawbee :bodle :plack :bawbee :bodle :plack :bawbee :bodle :plack :bawbee :bodle))]
      (is (= (vals (:coins changed)) '(0 3 3 3)) "each coin slot should be decremented by one")
      (is (= (vals (:coins emptied)) '(0 0 0 0)) "each coin slot should be empty (contain zero)")
      ))

    (testing
      "full machine cycle"
      (let [machine (get-caramel-wafer (add-coin (add-coin (make-default-machine) :bawbee) :bawbee))]
        (is (= (:message machine) "Enjoy your :caramel-wafer"))
        (is (= (set (:change machine)) #{:bodle}))
        (is (= (:bawbee (:coins machine)) 6))
        (is (= (:output machine) '(:caramel-wafer)))))
  )

