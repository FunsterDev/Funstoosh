module ApplicationCable
  class Connection < ActionCable::Connection::Base
    include Concerns::SessionManagement

    identified_by :current_user

    def connect
      reject_unauthorized_connection if not current_user
  end
end
