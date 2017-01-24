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

  validates_presence_of :players, :invite, on: :create

  after_create_commit :invite_users
  after_destroy_commit :delete_path

  def invite_users
    owner = players.first
    result = fcm.send(
      User.find_by_phone_numbers(invite).pluck(:fcm_registration_id),
      {
        collapse_key: 'invite',
        data: { game_id: id, from: owner.phone_number }
      }
    )
    logger.info "GCM response: #{result}"
  end

  def path
    @path ||= File.join("/tmp/pictures", id)
  end

  private

  def delete_path
    FileUtils.rm_rf(path)
  end
end
