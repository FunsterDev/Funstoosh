# == Schema Information
#
# Table name: users
#
#  id                  :integer          not null, primary key
#  country_code        :string(255)      not null
#  phone_number        :string(255)      not null
#  gcm_registration_id :string(255)      not null
#  game_id             :integer
#  created_at          :datetime         not null
#  updated_at          :datetime         not null
#
# Indexes
#
#  index_users_on_country_code_and_phone_number  (country_code,phone_number) UNIQUE
#  index_users_on_game_id                        (game_id)
#

require 'test_helper'

class UsersControllerTest < ActionDispatch::IntegrationTest
  # test "the truth" do
  #   assert true
  # end
end
