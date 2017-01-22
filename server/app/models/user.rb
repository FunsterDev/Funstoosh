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

class User < ApplicationRecord
  validates_presence_of :country_code, :phone_number
  validates_presence_of :gcm_registration_id

  before_validation :normalize!
  around_create :override_existing

  def self.normalize_phone(country_code, phone_number)
    if country_code
      country_code = country_code.gsub(/[^\D]/, '')
    end
    if phone_number
      phone_number = phone_number[1..-1] if phone_number.starts_with?('0')
      phone_number = phone_number.gsub(/[^\D]/, '')
    end

    phone_number = "#{country_code}#{phone_number}" if country_code

    [country_code, phone_number]
  end

  def find_by_phone_numbers(phone_numbers)
    phone_numbers = phone_numbers.map do |phone_number|
      country_code = self.country_code if phone_number.starts_with?('0')
      _, normalized = User.normalize_phone(country_code, phone_number)

      normalized
    end
    User.where(phone_numbers: phone_numbers)
  end

  def installed_phone_numbers(phone_numbers)
    map = Hash[
      phone_numbers.map do |phone_number|
        country_code = self.country_code if phone_number.starts_with?('0')
        _, normalized = User.normalize_phone(country_code, phone_number)

        [normalized, phone_number]
      end
    ]
    User.where(phone_number: map.keys).map do |user|
      map[user.phone_number]
    end
  end

  private

  def normalize!
    self.country_code, self.phone_number = User.normalize_phone(country_code, phone_number)
  end

  def override_existing
    gcm_registration_id = self.gcm_registration_id
    begin
      yield
    rescue ActiveRecord::RecordNotUnique
      other = User.where(country_code: country_code, phone_number: phone_number).first
      raise if other.nil? # some other error? will cause validation failure

      coder = {}
      other.encode_with(coder)
      init_with(coder)

      if gcm_registration_id != self.gcm_registration_id
        # updated
        self.gcm_registration_id = gcm_registration_id
        save!
      end
    end
  end
end
