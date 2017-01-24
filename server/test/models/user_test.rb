# == Schema Information
#
# Table name: users
#
#  id                  :integer          not null, primary key
#  country_code        :string(255)      not null
#  phone_number        :string(255)      not null
#  fcm_registration_id :string(255)      not null
#  game_id             :integer
#  created_at          :datetime         not null
#  updated_at          :datetime         not null
#
# Indexes
#
#  index_users_on_game_id       (game_id)
#  index_users_on_phone_number  (phone_number) UNIQUE
#

require 'test_helper'

class UserTest < ActiveSupport::TestCase
  # test "the truth" do
  #   assert true
  # end
end
