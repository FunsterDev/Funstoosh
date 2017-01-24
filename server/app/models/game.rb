# == Schema Information
#
# Table name: games
#
#  id         :integer          not null, primary key
#  created_at :datetime         not null
#  updated_at :datetime         not null
#

class Game < ApplicationRecord
  attr_accessor :invite

  has_many :users

  validates_presence_of :users, :invite, on: :create

  after_create_commit :invite_users
  after_destroy_commit :delete_path

  def invite_users
    owner = users.first
    result = FCM.client.send(
      owner.find_by_phone_numbers(invite).pluck(:fcm_registration_id),
      data: { type: 'invite', game_id: id, who: owner.phone_number }
    )
    logger.info "FCM response: #{result}"
    raise "FCM failed (game id #{id}): #{result}" if result[:status_code] != 200
  end

  def path
    @path ||= File.join("/tmp/funstoosh/pictures", id.to_s)
  end

  private

  def delete_path
    FileUtils.rm_rf(path)
  end
end
