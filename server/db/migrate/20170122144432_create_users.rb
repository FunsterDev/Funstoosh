class CreateUsers < ActiveRecord::Migration[5.0]
  def change
    create_table :users do |t|
      t.string :country_code, null: false
      t.string :phone_number, null: false
      t.string :gcm_registration_id, null: false

      t.references :game, foreign_key: { on_delete: :nullify }

      t.index [:country_code, :phone_number], unique: true

      t.timestamps
    end
  end
end
