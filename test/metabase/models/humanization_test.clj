(ns metabase.models.humanization-test
  (:require [expectations :refer :all]
            (metabase.models [humanization :refer :all]
                             [table :refer [Table]])
            [metabase.test.util :as tu]
            [metabase.db :as db]))

(tu/resolve-private-fns metabase.models.humanization
  name->human-readable-name:simple name->human-readable-name:advanced)

;;; name->human-readable-name:advanced
(expect nil                           (name->human-readable-name:advanced nil))
(expect nil                           (name->human-readable-name:advanced ""))
(expect ""                            (name->human-readable-name:advanced "_"))
(expect ""                            (name->human-readable-name:advanced "-"))
(expect "ID"                          (name->human-readable-name:advanced "_id"))
(expect "Agent Invite Migration"      (name->human-readable-name:advanced "_agent_invite_migration"))
(expect "Agent Invite Migration"      (name->human-readable-name:advanced "-agent-invite-migration"))
(expect "Foo Bar"                     (name->human-readable-name:advanced "fooBar"))
(expect "Foo Bar"                     (name->human-readable-name:advanced "foo-bar"))
(expect "Foo Bar"                     (name->human-readable-name:advanced "foo_bar"))
(expect "Foo Bar"                     (name->human-readable-name:advanced "foo bar"))
(expect "Dashboard Card Subscription" (name->human-readable-name:advanced "dashboardcardsubscription"))
(expect "Foo ID"                      (name->human-readable-name:advanced "foo_id"))
(expect "Receiver ID"                 (name->human-readable-name:advanced "receiver_id"))


;;; name->human-readable-name:simple
(expect nil                         (name->human-readable-name:simple nil))
(expect nil                         (name->human-readable-name:simple ""))
(expect ""                          (name->human-readable-name:simple "_"))
(expect ""                          (name->human-readable-name:simple "-"))
(expect "ID"                        (name->human-readable-name:simple "_id"))
(expect "Agent Invite Migration"    (name->human-readable-name:simple "_agent_invite_migration"))
(expect "Agent Invite Migration"    (name->human-readable-name:simple "-agent-invite-migration"))
(expect "Foobar"                    (name->human-readable-name:simple "fooBar"))
(expect "Foo Bar"                   (name->human-readable-name:simple "foo-bar"))
(expect "Foo Bar"                   (name->human-readable-name:simple "foo_bar"))
(expect "Foo Bar"                   (name->human-readable-name:simple "foo bar"))
(expect "Dashboardcardsubscription" (name->human-readable-name:simple "dashboardcardsubscription"))
(expect "Foo ID"                    (name->human-readable-name:simple "foo_id"))
(expect "Receiver ID"               (name->human-readable-name:simple "receiver_id"))


;;; Re-humanization

;; check that we get the expected :display_name with advanced humanization *enabled*
(expect
  "Toucans Are Cool"
  (tu/with-temporary-setting-values [enable-advanced-humanization true]
    (tu/with-temp* [Table [{table-id :id} {:name "toucansare_cool"}]]
      (db/select-one-field :display_name Table, :id table-id))))

;; check that we get the expected :display_name with advanced humanization *disabled*
(expect
  "Toucansare Cool"
  (tu/with-temporary-setting-values [enable-advanced-humanization false]
    (tu/with-temp* [Table [{table-id :id} {:name "toucansare_cool"}]]
      (db/select-one-field :display_name Table, :id table-id))))

;; now check that existing tables have their :display_names updated appropriately when the setting `enabled-advanced-humanization` is toggled
(expect
  ["Toucans Are Cool"
   "Toucansare Cool"
   "Toucans Are Cool"]
  (tu/with-temporary-setting-values [enable-advanced-humanization true]
    (tu/with-temp* [Table [{table-id :id} {:name "toucansare_cool"}]]
      (let [display-name #(db/select-one-field :display_name Table, :id table-id)]
        [(display-name)
         (do (enable-advanced-humanization false)
             (display-name))
         (do (enable-advanced-humanization true)
             (display-name))]))))
