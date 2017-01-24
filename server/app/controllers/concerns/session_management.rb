module Concerns::SessionManagement
  extend ActiveSupport::Concern

  def current_user
    @current_user ||= User.find_by_id(session[:user_id]) if session[:user_id]
  end

  def current_user=(user)
    @current_user = user
    session[:user_id] = user ? user.id : nil
  end

  def current_game
    current_user.game
  end

  def require_session
    head :forbidden if not current_user
  end

  def require_playing
    head :forbidden if not current_user.game_id
  end

  class_methods do
    def requires_session(**options)
      before_action :require_session, **options
    end

    def requires_playing(**options)
      before_action :require_playing, **options
    end
  end
end
