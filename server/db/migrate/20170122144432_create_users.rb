class CreateUsers < ActiveRecord::Migration[5.0]
  def change
    create_table :users do |t|
      t.string :country_code, null: false
      t.string :phone_number, null: false, index: { unique: true }
      t.string :fcm_registration_id, null: false

      t.references :game, foreign_key: { on_delete: :nullify }

      t.timestamps
    end
  end
end
