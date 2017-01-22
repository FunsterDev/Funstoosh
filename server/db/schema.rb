# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20170122144432) do

  create_table "games", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
  end

  create_table "users", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.string   "country_code",        null: false
    t.string   "phone_number",        null: false
    t.string   "gcm_registration_id", null: false
    t.integer  "game_id"
    t.datetime "created_at",          null: false
    t.datetime "updated_at",          null: false
    t.index ["country_code", "phone_number"], name: "index_users_on_country_code_and_phone_number", unique: true, using: :btree
    t.index ["game_id"], name: "index_users_on_game_id", using: :btree
  end

  add_foreign_key "users", "games", on_delete: :nullify
end
