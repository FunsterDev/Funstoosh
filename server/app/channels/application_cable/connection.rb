module ApplicationCable
  class Connection < ActionCable::Connection::Base
    identified_by :current_user

    def connect
      self.current_user = User.find_by_id(cookies.signed[:user_id])
      reject_unauthorized_connection if not current_user
    end
  end
end
