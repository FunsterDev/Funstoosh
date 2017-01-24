class UsersController < ApplicationController
  requires_session only: :index

  def create
    self.current_user = User.create!(user_params)
    head :created
  end

  def index
    render json: current_user.installed_phone_numbers(params[:phone_numbers].split(','))
  end

  private

  def user_params
    params.permit(:country_code, :phone_number, :fcm_registration_id)
  end
end
