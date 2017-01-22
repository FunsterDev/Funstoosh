class UsersController < ApplicationController
  requires_session only: :index

  def create
    self.current_user = User.create!(user_params)
    render nothing: true, status: :created
  end

  def index
    render json: {
      phone_numbers: current_user.installed_phone_numbers(params[:phone_numbers])
    }
  end

  private

  def user_params
    params.permit(:country_code, :phone_number, :gcm_registration_id)
  end
end
